<?php

require_once('for_php7.php');

require_once('knje063p_2Model.inc');
require_once('knje063p_2Query.inc');

class knje063p_2Controller extends Controller {
    var $ModelClassName = "knje063p_2Model";
    var $ProgramID      = "KNJE063P_2";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "class":
                case "add_year":
                case "subclasscd":
                    $this->callView("knje063p_2Form2");
                    break 2;
                case "right_list":
                case "list":
                case "sort":
                    $sessionInstance->getMainModel();
                    $this->callView("knje063p_2Form1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knje063p_2Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje063p_2Ctl = new knje063p_2Controller;
?>
