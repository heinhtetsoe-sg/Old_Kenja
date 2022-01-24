<?php

require_once('for_php7.php');

require_once('knja224dModel.inc');
require_once('knja224dQuery.inc');

class knja224dController extends Controller {
    var $ModelClassName = "knja224dModel";
    var $ProgramID      = "KNJA224D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja224d":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja224dModel();        //コントロールマスタの呼び出し
                    $this->callView("knja224dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja224dCtl = new knja224dController;
//var_dump($_REQUEST);
?>
