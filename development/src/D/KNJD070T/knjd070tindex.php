<?php

require_once('for_php7.php');

require_once('knjd070tModel.inc');
require_once('knjd070tQuery.inc');

class knjd070tController extends Controller {
    var $ModelClassName = "knjd070tModel";
    var $ProgramID      = "KNJD070t";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd070t":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd070tModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd070tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd070tCtl = new knjd070tController;
var_dump($_REQUEST);
?>
