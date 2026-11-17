select from llm
where factId = 'Inventory:INV-001'
and relatedFactIds = 'Sales'
expression = '(Inventory.quantityOnHand - Inventory.quantityReserved) / Sales.quantitySold'
definition = 'inventory availability.'
