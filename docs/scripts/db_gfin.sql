use db_gfin;

db.createUser({
    user: 'user_gfin_app',
    pwd: '2026_user_gfin',
    roles: [
        {
            role: 'readWrite',
            db: 'db_gfin'
            }
        ]
    });