#!/usr/bin/env ruby

# This script can be used to generate the handlebars template for application form

f = {}
f["last_name"]="Last Name"
f["first_name"]="First Name"
f["middle_name"]="Middle Name"
f["ssn"]="SSN"
f["other_names"]="Other Names"
f["home_phone"]="Home Phone Number"
f["work_phone"]="Work Phone Number"
f["cell_phone"]="Cell Phone Number"
f["dob"]="Date of Birth"
f["email"]="Email"
f["id_type"]="Photo ID/Type"
f["id_number"]="ID Number"
f["id_issuer"]="ID Issuing Government"
f["id_exeration"]="ID Exp. Date"

f.each do |id, label|
puts "<div class=\"form-group\">"
puts "    <label for=\"#{id}\">#{label}</label>"
puts "    <input type=\"text\" class=\"form-control\" id=\"#{id}\" name=\"#{id}\" value=\"{{#{id}}}\">"
puts "</div>"
end
