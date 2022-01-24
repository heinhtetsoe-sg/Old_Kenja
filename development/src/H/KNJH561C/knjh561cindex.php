<?php

require_once('for_php7.php');

require_once('knjh561cModel.inc');
require_once('knjh561cQuery.inc');

class knjh561cController extends Controller {
    var $ModelClassName = "knjh561cModel";
    var $ProgramID      = "KNJH561C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh561c":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh561cModel();       //コントロールマスタの呼び出し
                    $this->callView("knjh561cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh561cCtl = new knjh561cController;
//var_dump($_REQUEST);
?>
