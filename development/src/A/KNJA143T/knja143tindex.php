<?php

require_once('for_php7.php');

require_once('knja143tModel.inc');
require_once('knja143tQuery.inc');

class knja143tController extends Controller {
    var $ModelClassName = "knja143tModel";
    var $ProgramID      = "KNJA143T";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "changeGrade":
                case "knja143t":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja143tModel();        //コントロールマスタの呼び出し
                    $this->callView("knja143tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja143tCtl = new knja143tController;
//var_dump($_REQUEST);
?>

