# 매칭 시스템 

### 주문 대기 데이터를 Redis 에 sorted sets 자료 구조로 저장 

- 키 : orderbook.{buy:sell}.{ticker} - string 
- value : {"orderId":UUId, "quantity":100, "price":20000.00}
- score : price - double