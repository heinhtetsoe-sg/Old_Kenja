<?php

require_once('for_php7.php');

require_once('knja210aModel.inc');
require_once('knja210aQuery.inc');

class knja210aController extends Controller {
    var $ModelClassName = "knja210aModel";
    var $ProgramID      = "KNJA210A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knja210a":
                    $sessionInstance->knja210aModel();
                    $this->callView("knja210aForm1");
                    exit;
                case "dsub":
                    $this->callView("knja210aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja210aCtl = new knja210aController;
?>
