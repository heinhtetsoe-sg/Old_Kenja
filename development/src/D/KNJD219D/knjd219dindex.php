<?php

require_once('for_php7.php');

require_once('knjd219dModel.inc');
require_once('knjd219dQuery.inc');

class knjd219dController extends Controller {
    var $ModelClassName = "knjd219dModel";
    var $ProgramID      = "KNJD219D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjd219d");
                    break 1;
                case "":
                case "knjd219d":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd219dModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd219dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd219dCtl = new knjd219dController;
//var_dump($_REQUEST);
?>
