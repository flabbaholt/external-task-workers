```markdown
# Dockerized MySQL for External Task Workers

This project includes a prebuilt MySQL Docker image (`my-mysql-logistics.tar`) with preloaded data for the `logistics` database. Follow the steps below to set it up and run it locally.

## Prerequisites
Before you begin, ensure you have:
- **Docker** installed on your machine. You can download it [here](https://docs.docker.com/get-docker/).

---

## Steps to Run

### 1. Load the Docker Image
Use the following command to load the prebuilt Docker image from the `.tar` file:
```bash
docker load -i docker/my-mysql-logistics.tar
```

### 2. Run the MySQL Container
Start a new container using the loaded Docker image:
```bash
docker run --name mysql-docker -d -p 3306:3306 my-mysql-logistics
```

### 3. Connect to the MySQL Database
Use the following credentials to connect to the database:

- **Host**: `localhost`
- **Port**: `3306`
- **User**: `root`
- **Password**: `root`
- **Database**: `logistics`

You can connect using:
- **MySQL Workbench**.
- Any MySQL client.
- Your application.

---

## Managing the Container
### Stop the Container
To stop the running container:
```bash
docker stop mysql-docker
```

### Start the Container
To start the stopped container:
```bash
docker start mysql-docker
```

### Remove the Container
To remove the container completely:
```bash
docker rm mysql-docker
```

---

## Notes
- The `logistics` database comes preloaded with sample data.
- Ensure that port `3306` is not being used by another application or container before starting.

If you encounter any issues, feel free to reach out.
```

