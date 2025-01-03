db.createUser({
    user: 'ecommerceUser',
    pwd: 'ecommercePass',
    roles: [
        {
            role: 'readWrite',
            db: 'ecommerce'
        }
    ]
});

db = db.getSiblingDB('ecommerce');

// Create products collection with validation
db.createCollection('products', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['productId', 'name', 'price', 'inStock'],
            properties: {
                productId: {
                    bsonType: 'string',
                    description: 'must be a string and is required'
                },
                name: {
                    bsonType: 'string',
                    description: 'must be a string and is required'
                },
                price: {
                    bsonType: 'double',
                    description: 'must be a double and is required'
                },
                inStock: {
                    bsonType: 'bool',
                    description: 'must be a boolean and is required'
                }
            }
        }
    }
});

// Insert some sample data
db.products.insertMany([
    {
        productId: "PROD001",
        name: "Gaming Laptop",
        price: 1299.99,
        inStock: true
    },
    {
        productId: "PROD002",
        name: "Wireless Mouse",
        price: 49.99,
        inStock: true
    }
]);