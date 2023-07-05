import postgres from 'postgres'

const postgresClient = postgres(process.env.POSTGRES_DATABASE_URL as string);

export default postgresClient;