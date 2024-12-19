# 동시성 제어 방식 보고서

## 개요

`LockManager`을 통해 `PointService`의 서비스 로직에 대해 동시성 제어를 구현하였습니다.
구현 방식과 검증을 위해 작성한 테스트 코드에 대해 설명합니다.

---

## 동시성 제어

동시성을 제어한다는 측면에서 데이터베이스를 모방하여 구현하였습니다. PK를 통해 레코드에 접근하는
클러스터링 인덱스에서 아이디어를 얻어 HashMap으로 구현하였고, Serializable의 격리 수준에서 각 데이터에 접근 시 S Lock과 X Lock을 획득하는 것을 모방하여
ReentrantReadWriteLock을 사용하였습니다.

### LockManager

여러 스레드에서 접근해야하기 때문에 `ConcurrentHashMap을` 사용하였고,
key로 유저의 Id - value로 `ReentrantReadWriteLock을` 사용했습니다.
추가적으로 새로 접근하는 유저의 경우 Id에 해당하는 Lock이 없기 때문에,
생성자 호출 과정을 `synchronized`로 묶어 약간의 시간 차이로 인한 오차를 배제하였습니다.

read와 write 호출 메서드를 분리하여 각각 ReentrantReadWriteLock의 read를 통해 S Lock 방식을 구현,
ReentrantReadWriteLock의 write를 통해 X Lock 방식을 구현했습니다.
또한 공정 모드를 활성화하여 받은 요청에 대해 순차적으로 처리되도록 하였습니다.

### PointService

`PointService`에서는 LockManager에서 구현된 read와 write 메서드를 통해 동시성 제어를 수행하도록 만들었습니다.
서로 다른 테이블인 UserPointTable과 PointHistoryTable에는 경쟁 상태를 유발하지 않아야 하기 때문에,
LockManager를 `userPointLockManager`와 `pointHistoryLockManager`로 분리하여 선언해두었습니다.

`userPointLockManager`는 포인트 조회 시에 read를 호출하고, 포인트 충전이나 사용 시 write를 호출하여 동시성을 제어합니다.
`pointHistoryLockManager`는 포인트 내역 조회시 read, 포인트 충전과 사용 시 포인트 내역을 저장하는 경우 write를 호출하였습니다.


---

## 동시성 테스트

### LockManager 단위 테스트

LockManager의 동작을 검증하기 위한 단위 테스트를 작성했습니다.

1. 동일 id Read Lock 경쟁 테스트
   기본적으로 S Lock은 서로 블로킹하지 않습니다. 이를 테스트하기 위해 `Thread.sleep`을 통해 요청 처리소요 시간을 늘리고,
   여러 read 요청을 보냈을 때 경쟁 없이 병렬 처리가 되는지 확인했습니다.

2. 동일 id Write Lock 경쟁 테스트
   X Lock은 서로 블로킹합니다. 동일하게 `Thread.sleep`을 통해 요청 처리소요 시간을 늘리고,
   서로 경쟁하여 병렬처리 되지 않고 전체 시간이 각 소요시간의 합보다 큰 지 확인했습니다.

3. 서로 다른 id Write Lock 경쟁 테스트
   서로 다른 id를 가진다면 각각 자신의 Write Lock을 얻어 병렬적으로 처리될 것이므로,
   이를 활용하여 전체 시간이 요청 중 최대 처리 시간과 동일한지 확인했습니다.

4. 동일 id Read - Write Lock 경쟁 테스트
   S Lock과 X Lock은 서로 경쟁합니다. 먼저 S Lock 요청을 여러 번 보내고, 이후 X Lock 획득 요청을 보내어
   처리 시간이 S Lock 중 최대 소요시간 + 모든 X Lock 소요시간의 합만큼 걸리는지 확인했습니다.

### PointService 동시성 통합 테스트

발생할 수 있는 동시성 문제 상황을 통합 테스트를 진행했습니다.

1. 동일한 유저의 충전 동시 요청
   동일한 유저가 많은 수의 충전 요청을 동시에 요청 했을 때,
   최종적으로 충전되어있는 포인트가 (충전 포인트 x 충전 요청 수)만큼 되어있고
   그에 대한 충전 포인트 내역이 잘 저장되어있는지 검증했습니다.

2. 서로 다른 유저의 충전 동시 요청
   서로 다른 유저들로 동시에 충전 요청을 했을 때,
   서로 경쟁 없이 빠른 시간 내에 처리 되었는지와 각자 충전 및 충전 내역이 잘 저장되어있는지 검증햇습니다.

3. 동일한 유저의 사용 동시 요청
   동일한 유저의 포인트 사용 동시 요청에 대해서도
   포인트 충전과 동일한 방식으로 서비스 로직에 대한 동시성을 검증했습니다.

4. 서로 다른 유저의 사용 동시 요청
   서로 다른 유저의 동시 사용 요청에 대해서도 충전과 동일하게 검증했습니다.

5. 충전과 사용의 순차 요청 시 순서 보장
   여러 건의 충전 요청 이후 약간(1ms)의 차이를 두고 충전된 포인트에 대해서 사용 시도를 했을 때,
   공정모드로 인해 요청이 순차적으로 충전이 처리되어 포인트 사용에 성공하는지를 검증했습니다.
