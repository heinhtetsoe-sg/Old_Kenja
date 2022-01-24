<?php

require_once('for_php7.php');

require_once('knjl252cModel.inc');
require_once('knjl252cQuery.inc');

class knjl252cController extends Controller {
    var $ModelClassName = "knjl252cModel";
    var $ProgramID      = "KNJL252C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl252c":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl252cModel();       //コントロールマスタの呼び出し
                    $this->callView("knjl252cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl252cCtl = new knjl252cController;
var_dump($_REQUEST);
?>
