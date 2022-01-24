<?php

require_once('for_php7.php');

require_once('knjx_guarantor_addressModel.inc');
require_once('knjx_guarantor_addressQuery.inc');

class knjx_guarantor_addressController extends Controller
{
    public $ModelClassName = "knjx_guarantor_addressModel";
    public $ProgramID      = "KNJA110A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "list2":
                    $this->callView("knjx_guarantor_addressForm1");
                    break 2;
                case "list":
                    $this->callView("knjx_guarantor_addressForm1");
                    break 2;
                case "edit":
                case "clear":
                case "back":
                    $this->callView("knjx_guarantor_addressForm1");
                    break 2;
                case "subForm1":
                    $this->callView("knjx_guarantor_addressSubForm1");
                    break 2;
                case "add2":
                case "subAdd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                case "jyuushorireki":
                case "edit2":
                    $this->callView("knjx_guarantor_addressForm1");
                    return;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_guarantor_addressCtl = new knjx_guarantor_addressController();
