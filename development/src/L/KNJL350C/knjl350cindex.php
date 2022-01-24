<?php

require_once('for_php7.php');

require_once('knjl350cModel.inc');
require_once('knjl350cQuery.inc');

class knjl350cController extends Controller {
    var $ModelClassName = "knjl350cModel";
    var $ProgramID      = "KNJL350C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl350c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl350cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl350cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl350cCtl = new knjl350cController;
//var_dump($_REQUEST);
?>
