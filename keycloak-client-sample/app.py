from flask import Flask, request, redirect, render_template_string

app = Flask(__name__)

@app.route('/', methods=['GET', 'POST'])
def index():
    if request.method == 'POST':
        realm_name = request.form['realm-name']
        client_id = request.form['client-id']
        redirect_uri = f"http://localhost:8080/realms/{realm_name}/protocol/openid-connect/auth?client_id={client_id}&response_type=code&redirect_uri=http://127.0.0.1:5151/callback&scope=openid"
        return redirect(redirect_uri)
    return render_template_string(template)

@app.route('/callback', methods=['GET'])
def callback():
    code = request.args.get('code')
    return f"Authorization Code: {code}"

template = '''
<!doctype html>
<html lang="en">
  <head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <title>Keycloak Authorization</title>
  </head>
  <body>
    <div class="container">
      <h1 class="mt-5">Enter Realm and Client ID</h1>
      <form method="post">
        <div class="mb-3">
          <label for="realm-name" class="form-label">Realm Name</label>
          <input type="text" class="form-control" id="realm-name" name="realm-name" required>
        </div>
        <div class="mb-3">
          <label for="client-id" class="form-label">Client ID</label>
          <input type="text" class="form-control" id="client-id" name="client-id" required>
        </div>
        <button type="submit" class="btn btn-primary">Submit</button>
      </form>
    </div>
    <!-- Optional JavaScript; choose one of the two! -->
    <!-- Option 1: Bootstrap Bundle with Popper -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"></script>
  </body>
</html>
'''

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)