import yfinance as yf
import psycopg2
from psycopg2 import sql
from datetime import datetime

# PostgreSQL 연결 정보
DB_HOST = "localhost"
DB_NAME = "axelrod"
DB_USER = "sangyoon"
DB_PASSWORD = None

# 티커 리스트
tickers = [
    "AAPL", "MSFT", "AMZN", "TSLA", "GOOG",
    "FB", "NVDA", "JPM", "BAC", "XOM",
    "WMT", "V", "KO", "PFE", "NFLX"
]

# PostgreSQL에 연결
def connect_to_db():
    try:
        connection = psycopg2.connect(
            host=DB_HOST,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )
        return connection
    except Exception as e:
        print(f"Error connecting to the database: {e}")
        return None

# 주식 정보를 Stocks 테이블에 삽입
def insert_stock_data(connection, stock_data):
    try:
        with connection.cursor() as cursor:
            query = sql.SQL("""
                INSERT INTO Stock (ticker, name, market, sector, price, time_stamp)
                VALUES (%s, %s, %s, %s, %s, %s)
                ON CONFLICT (ticker) DO NOTHING
            """)
            cursor.execute(query, stock_data)
        connection.commit()
    except Exception as e:
        print(f"Error inserting data: {e}")

# 티커 리스트로 주식 정보 가져오기 및 DB 삽입
def fetch_and_store_stocks():
    connection = connect_to_db()
    if not connection:
        return

    for ticker in tickers:
        try:
            # Yahoo Finance에서 주식 데이터 가져오기
            stock = yf.Ticker(ticker)
            info = stock.info

            # 필요한 데이터 추출
            stock_data = (
                ticker,
                info.get("shortName"),
                info.get("market"),
                info.get("sector"),
                info.get("regularMarketPrice"),
                datetime.now()
            )

            # DB에 삽입
            insert_stock_data(connection, stock_data)
            print(f"Inserted data for {ticker}")
        except Exception as e:
            print(f"Error fetching data for {ticker}: {e}")

    connection.close()

# 실행
if __name__ == "__main__":
    fetch_and_store_stocks()
