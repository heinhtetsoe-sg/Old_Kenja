<?php

require_once('for_php7.php');

require_once('knjf030eModel.inc');
require_once('knjf030eQuery.inc');

class knjf030eController extends Controller {
    var $ModelClassName = "knjf030eModel";
    var $ProgramID        = "KNJF030E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf030e":                                //メニュー画面もしくはSUBMITした場合
                case "change_class":                        //クラス変更時のSUBMITした場合
                    $sessionInstance->knjf030eModel();      //コントロールマスタの呼び出し
                    $this->callView("knjf030eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjf030eCtl = new knjf030eController;
var_dump($_REQUEST);
?>
