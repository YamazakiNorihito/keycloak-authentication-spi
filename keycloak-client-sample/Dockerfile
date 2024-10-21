# Dockerfile
FROM python:3.9

# 作業ディレクトリを設定
WORKDIR /app

# 必要なファイルをコンテナにコピー
COPY requirements.txt requirements.txt
COPY app.py app.py

# 依存関係をインストール
RUN pip install -r requirements.txt

# アプリケーションを実行
CMD ["python", "app.py"]


# docker build -t flask-keycloak-app .
# docker run -p 5151:5000 --rm flask-keycloak-app