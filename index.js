function fetchData() {
  return Promise.resolve()
    .then(_ => "some data!")
}
const f = fetchData()
f.then(data =>
  console.log(data)
)
