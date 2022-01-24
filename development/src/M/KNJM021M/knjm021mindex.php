<?php

require_once('for_php7.php');

require_once('knjm021mModel.inc');
require_once('knjm021mQuery.inc');

class knjm021mController extends Controller {
    var $ModelClassName = "knjm021mModel";
    var $ProgramID      = "KNJM021M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm021m":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm021mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm021mForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjm021mModel();
                    $this->callView("knjm021mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm021mCtl = new knjm021mController;
var_dump($_REQUEST);
?>
