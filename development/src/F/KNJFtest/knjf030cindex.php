<?php

require_once('for_php7.php');

require_once('knjf030cModel.inc');
require_once('knjf030cQuery.inc');

class knjf030cController extends Controller {
    var $ModelClassName = "knjf030cModel";
    var $ProgramID        = "KNJF030C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf030c":                                //メニュー画面もしくはSUBMITした場合
                case "change_class":                        //クラス変更時のSUBMITした場合
                    $sessionInstance->knjf030cModel();      //コントロールマスタの呼び出し
                    $this->callView("knjf030cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjf030cCtl = new knjf030cController;
var_dump($_REQUEST);
?>
