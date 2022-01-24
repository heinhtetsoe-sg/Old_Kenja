<?php

require_once('for_php7.php');

require_once('knja121c_shokenModel.inc');
require_once('knja121c_shokenQuery.inc');

class knja121c_shokenController extends Controller {
    var $ModelClassName = "knja121c_shokenModel";
    var $ProgramID      = "KNJA121C_SHOKEN";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "":
                    $sessionInstance->knja121c_shokenModel();
                    $this->callView("knja121c_shokenForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja121c_shokenCtl = new knja121c_shokenController;
?>
