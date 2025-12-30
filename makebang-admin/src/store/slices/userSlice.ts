import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import type { User } from '@/types'

interface UserState {
  currentUser: User | null
  token: string | null
  isLoggedIn: boolean
  loading: boolean
}

const initialState: UserState = {
  currentUser: null,
  token: localStorage.getItem('admin_token'),
  isLoggedIn: !!localStorage.getItem('admin_token'),
  loading: false,
}

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    setUser: (state, action: PayloadAction<User>) => {
      state.currentUser = action.payload
      state.isLoggedIn = true
    },
    setToken: (state, action: PayloadAction<string>) => {
      state.token = action.payload
      state.isLoggedIn = true
      localStorage.setItem('admin_token', action.payload)
    },
    logout: (state) => {
      state.currentUser = null
      state.token = null
      state.isLoggedIn = false
      localStorage.removeItem('admin_token')
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload
    },
  },
})

export const { setUser, setToken, logout, setLoading } = userSlice.actions
export default userSlice.reducer
