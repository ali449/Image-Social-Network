echo "Building [1] discovery service image..."
docker build -t discovery-service -f ./../eureka/Dockerfile ./../eureka

echo "Building [2] user service image..."
docker build -t user-service -f ./../gateway/Dockerfile ./../gateway

echo "Building [3] post service image..."
docker build -t post-service -f ./../post/Dockerfile ./../post

echo "Building [4] recommender service image..."
docker build -t recommender-service -f ./../recommender/Dockerfile ./../recommender

echo "Building [5] score service image..."
docker build -t score-service -f ./../score/Dockerfile ./../score

echo Completed.