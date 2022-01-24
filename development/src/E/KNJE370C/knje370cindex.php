<?php

require_once('for_php7.php');

require_once('knje370cModel.inc');
require_once('knje370cQuery.inc');

class knje370cController extends Controller {
    var $ModelClassName = "knje370cModel";
    var $ProgramID      = "KNJE370C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje370c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje370cModel();        //コントロールマスタの呼び出し
                    $this->callView("knje370cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje370cCtl = new knje370cController;
var_dump($_REQUEST);
?>
