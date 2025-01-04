# 매칭 시스템 

### 주문 대기 데이터를 Redis 에 sorted sets 자료 구조로 저장 

- 키 : orderbook.{buy:sell}.{ticker} - string 
- value : {"orderId":UUId, "quantity":100, "price":20000.00}
- score : price - double
- algorithm : Price/Time algorithm - 추후에 여러 알고리즘 비교 예정

(출처 : Order matching system Wikipedia)
![img.png](img.png)