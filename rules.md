{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null && auth.uid == $uid",
        "profile": {
          "data": {
            ".read": true,
            ".validate": "newData.hasChildren(['username'])"
          }
        },
        "accounts": {
        //   "$accountId": {
            // ".validate": "newData.hasChildren(['id', 'name', 'type'])"
        //   }
        },
        "categories": {
          "$categoryId": {
            ".validate": "newData.hasChildren(['id', 'name', 'type'])"
          }
        },
        "transactions": {
          ".indexOn": ["date", "type"],
          "$transactionId": {
            ".validate": "newData.hasChildren(['id', 'amount', 'type'])"
          }
        }
      }
    },
    
    "usernames": {
      "$username": {
        ".read": true,
        // ALLOWS:
        // 1. Create: If it doesn't exist and the new UID is yours
        // 2. Delete: If you own the existing entry (writing null)
        // 3. Update: If you own the existing entry and the new UID is still yours
        ".write": "auth != null && ( (!data.exists() && newData.child('uid').val() == auth.uid) || (data.child('uid').val() == auth.uid && (!newData.exists() || newData.child('uid').val() == auth.uid)) )",
        ".validate": "newData.hasChildren(['uid', 'username'])"
      }
    }
  }
}