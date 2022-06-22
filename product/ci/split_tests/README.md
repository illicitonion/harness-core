# split_tests

Splits a test suite into groups of equal time, based on previous tests timings.

This is necessary for running the tests in parallel. As the execution time of test files might vary drastically, you will not get the best split by simply dividing them into even groups.

## Compatibility

This tool was written for Ruby and CircleCI, but it can be used with any file-based test suite on any CI.
Since then, CircleCI has introduced built-in test splitting. Also since then, the tool has been applied on
GitHub Actions, that does not provide native test splitting.

There is a [split-tests GitHub Action](https://github.com/marketplace/actions/split-tests) using this tool available on the Actions Marketplace.

It is written in Golang, released as a binary, and has no external dependencies.

### Using a JUnit report

```
rspec $(split_tests -junit -junit-path=report.xml -split-index=$HARNESS_NODE_INDEX -split-total=$HARNESS_NODE_TOTAL)
```

Or, if it's easier to pipe the report file:

```
rspec $(curl http://my.junit.url | split_tests -junit -split-index=$HARNESS_NODE_INDEX -split-total=$HARNESS_NODE_TOTAL)
```

### Naive split by line count

If you don't have test times, it might be reasonable for your project to assume runtime proportional to test length.

```
rspec $(split_tests -line-count)
```

### Naive split by file count

In the absence of prior test times, `split_tests` can still split files into even groups by count.

```
rspec $(split_tests)
```

## Arguments

```plain
$./split_tests -help

Usage: split_tests [--glob GLOB] [--exclude-glob EXCLUDE-GLOB] [--split-index SPLIT-INDEX] [--split-total SPLIT-TOTAL] [--split-by-linecount] [--use-junit] [--junit-path JUNIT-PATH] [--verbose]

Options:
  --glob GLOB            Glob pattern to find the test files
  --exclude-glob EXCLUDE-GLOB
                         Glob pattern to exclude test files
  --split-index SPLIT-INDEX
                         Index of the current split (or set HARNESS_NODE_INDEX) [default: -1]
  --split-total SPLIT-TOTAL
                         Total number of splits (or set HARNESS_NODE_TOTAL) [default: -1]
  --split-by-linecount   Use line count to estimate test times [default: true]
  --use-junit            Use junit XML for test times
  --junit-path JUNIT-PATH
                         Path to Junit XML file to read test times
  --verbose              Enable verbose logging mode
  --help, -h             display this help and exit
```

## Compilation

This tool is written in Go and uses Go modules.

- Install Go
- Checkout the code
- `make`

With bazel: `bazel build //product/ci/split_tests/...`
