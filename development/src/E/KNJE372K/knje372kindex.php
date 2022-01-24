<?php

require_once('for_php7.php');

require_once('knje372kModel.inc');
require_once('knje372kQuery.inc');

class knje372kController extends Controller {
    var $ModelClassName = "knje372kModel";
    var $ProgramID      = "KNJE372K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje372k":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                    //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje372kModel();          //コントロールマスタの呼び出し
                    $this->callView("knje372kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje372kCtl = new knje372kController;
//var_dump($_REQUEST);
?>
