import React, { Component } from "react";
import { Link } from "react-router-dom";
import { getAllOrders, deleteOrder } from "../../../controllers/OrdersController";
import { getAllUsers } from "../../../controllers/UsersController";
import Order from "../../../models/Order";
import User from "../../../models/User";

type OrdersListState = {
    orders: Order[],
    usersMap: { [id: number]: User },
}

class OrdersList extends Component {

    state: OrdersListState = {
        orders: [],
        usersMap: {},
    }

    async componentDidMount() {
        const orders = await getAllOrders();
        const users = await getAllUsers();
        const usersMap: { [id: number]: User } = {};
        users.forEach(user => { usersMap[user.id] = user });
        this.setState({
            orders,
            usersMap,
        })
    }

    render() {
        const orderRows = this.state.orders.map((order, idx) => {
            const user = this.state.usersMap[order.userId];
            return (
                <tr key={idx}>
                    <th>{order.id}</th>
                    <td>{user.name} {user.name2}</td>
                    <td><Link to={`/order/${order.id}`} >Details</Link></td>
                    <td><button onClick={this.delete.bind(this, order.id)}>Delete</button></td>
                </tr>
            )
        })
        return (
            <div>
                <table>
                    <thead>
                        <tr>
                            <th>Order ID</th>
                            <th>Client</th>
                            <th></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {orderRows}
                    </tbody>
                </table>
            </div>
        )
    }

    async delete(id: number) {
        deleteOrder(id)
            .then(_ => {
                this.componentDidMount();
            })
            .catch(_ => {
                console.error("Failed to remove.");
            })
    }
}

export default OrdersList;
