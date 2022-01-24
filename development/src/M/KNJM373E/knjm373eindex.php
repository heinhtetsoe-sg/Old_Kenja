<?php

require_once('for_php7.php');

require_once('knjm373eModel.inc');
require_once('knjm373eQuery.inc');

class knjm373eController extends Controller {
    var $ModelClassName = "knjm373eModel";
    var $ProgramID      = "KNJM373E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm373e":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm373eModel();       //コントロールマスタの呼び出し
                    $this->callView("knjm373eForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjm373eModel();
                    $this->callView("knjm373eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm373eCtl = new knjm373eController;
var_dump($_REQUEST);
?>
