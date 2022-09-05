Oftentimes we want to build our apps as a set of isolated sections.

For example, let's say we want to have a "dashboard" section and an "account" section:

```scala
def DashboardSection(): Element =
  div("Dashboard")
  
def AccountSection(): Element =
  div("Account")
  

div(
  path("dashboard") {
    DashboardSection()
  },
  path("account") {
    AccountSection()
  }
)
```

And this works fine until we realize we also want to have sub-routes inside those sections, like the following: 

```
/dashboard
/dashboard/settings
/dashboard/alerts
/account
/account/settings
/account/billing
```

...and we want it to be isolated – we don't want to describe all the possible routes in one place and somehow notify the section component
which sub-route needs to be rendered.

`frontroute` supports nested routes, so we can easily implement this. First, we'll change the top-level routes to be `pathPrefix` instead 
of `path`:

```scala
div(
  pathPrefix("dashboard") {
    DashboardSection()
  },
  pathPrefix("account") {
    AccountSection()
  }
)
```

Now, inside our section components, we can seamlessly have nested routes:

```scala
def DashboardSection(): Element =
  div(
    "Dashboard"
    pathEnd {
      div(
        "Dashboard Home"
      )
    },
    path("settings") {
      div(
        "Dashboard Overview"
      )
    },
    path("alerts") {
      div(
        "Dashboard Alerts"
      )      
    }    
  )

def AccountSection(): Element =
  div(
    "Account",
    pathEnd {
      div(
        "Account Home"
      )
    },
    path("settings") {
      div(
        "Account Overview"
      )
    },
    path("billing") {
      div(
        "Account Alerts"
      )
    }
  )
```

This will work as you would (hopefully) expect it to work:

When the top-level route is evaluated, it will consume the first segment of the `path` – either the `dashboard` segment, 
or the `account` segment. So when it's time to evaluate the nested routes inside one of the section components,
the routes will see the path segments without the first segment, which will have been consumed by the top-level route. 

See [live example](/examples/nested-routes/live).
