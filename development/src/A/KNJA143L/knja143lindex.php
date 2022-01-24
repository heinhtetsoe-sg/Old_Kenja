<?php

require_once('for_php7.php');

require_once('knja143lModel.inc');
require_once('knja143lQuery.inc');

class knja143lController extends Controller {
    var $ModelClassName = "knja143lModel";
    var $ProgramID      = "KNJA143L";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja143l":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja143lModel();      //コントロールマスタの呼び出し
                    $this->callView("knja143lForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja143lCtl = new knja143lController;
//var_dump($_REQUEST);
?>
