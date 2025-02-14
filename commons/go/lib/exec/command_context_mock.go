// Copyright 2022 Harness Inc. All rights reserved.
// Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
// that can be found in the licenses directory at the root of this repository, also available at
// https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.

// Code generated by MockGen. DO NOT EDIT.
// Source: command_context.go

// Package exec is a generated GoMock package.
package exec

import (
	context "context"
	reflect "reflect"
	time "time"

	gomock "github.com/golang/mock/gomock"
)

// MockCmdContextFactory is a mock of CmdContextFactory interface.
type MockCmdContextFactory struct {
	ctrl     *gomock.Controller
	recorder *MockCmdContextFactoryMockRecorder
}

// MockCmdContextFactoryMockRecorder is the mock recorder for MockCmdContextFactory.
type MockCmdContextFactoryMockRecorder struct {
	mock *MockCmdContextFactory
}

// NewMockCmdContextFactory creates a new mock instance.
func NewMockCmdContextFactory(ctrl *gomock.Controller) *MockCmdContextFactory {
	mock := &MockCmdContextFactory{ctrl: ctrl}
	mock.recorder = &MockCmdContextFactoryMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockCmdContextFactory) EXPECT() *MockCmdContextFactoryMockRecorder {
	return m.recorder
}

// CmdContext mocks base method.
func (m *MockCmdContextFactory) CmdContext(ctx context.Context, name string, args ...string) Command {
	m.ctrl.T.Helper()
	varargs := []interface{}{ctx, name}
	for _, a := range args {
		varargs = append(varargs, a)
	}
	ret := m.ctrl.Call(m, "CmdContext", varargs...)
	ret0, _ := ret[0].(Command)
	return ret0
}

// CmdContext indicates an expected call of CmdContext.
func (mr *MockCmdContextFactoryMockRecorder) CmdContext(ctx, name interface{}, args ...interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	varargs := append([]interface{}{ctx, name}, args...)
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "CmdContext", reflect.TypeOf((*MockCmdContextFactory)(nil).CmdContext), varargs...)
}

// CmdContextWithSleep mocks base method.
func (m *MockCmdContextFactory) CmdContextWithSleep(ctx context.Context, sleep time.Duration, name string, args ...string) Command {
	m.ctrl.T.Helper()
	varargs := []interface{}{ctx, sleep, name}
	for _, a := range args {
		varargs = append(varargs, a)
	}
	ret := m.ctrl.Call(m, "CmdContextWithSleep", varargs...)
	ret0, _ := ret[0].(Command)
	return ret0
}

// CmdContextWithSleep indicates an expected call of CmdContextWithSleep.
func (mr *MockCmdContextFactoryMockRecorder) CmdContextWithSleep(ctx, sleep, name interface{}, args ...interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	varargs := append([]interface{}{ctx, sleep, name}, args...)
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "CmdContextWithSleep", reflect.TypeOf((*MockCmdContextFactory)(nil).CmdContextWithSleep), varargs...)
}
