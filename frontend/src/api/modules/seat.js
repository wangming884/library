import request from '../request'

export const getAvailableSeats = (params) => request.get('/seat/available', { params })
export const reserveSeat = (data) => request.post('/reader/seat/reserve', data)
export const checkInSeat = (id) => request.post(`/seat/checkin/${id}`)
export const releaseSeat = (id) => request.post(`/seat/release/${id}`)
export const getSeatReservations = (params) => request.get('/admin/seat-reservations', { params })
export const mySeatReservations = (params) => request.get('/reader/seat-reservations', { params })
export const addSeat = (data) => request.post('/admin/seats', data)
