<?php

require_once('for_php7.php');

require_once('knjx_guardian_addressModel.inc');
require_once('knjx_guardian_addressQuery.inc');

class knjx_guardian_addressController extends Controller
{
    public $ModelClassName = "knjx_guardian_addressModel";
    public $ProgramID      = "KNJA110A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "list2":
                    $this->callView("knjx_guardian_addressForm1");
                    break 2;
                case "list":
                    $this->callView("knjx_guardian_addressForm1");
                    break 2;
                case "edit":
                case "clear":
                case "back":
                    $this->callView("knjx_guardian_addressForm1");
                    break 2;
                case "subForm1":
                    $this->callView("knjx_guardian_addressSubForm1");
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
                    $this->callView("knjx_guardian_addressForm1");
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
$knjx_guardian_addressCtl = new knjx_guardian_addressController();
