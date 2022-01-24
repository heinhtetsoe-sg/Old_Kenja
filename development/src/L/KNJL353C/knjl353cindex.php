<?php

require_once('for_php7.php');

require_once('knjl353cModel.inc');
require_once('knjl353cQuery.inc');

class knjl353cController extends Controller {
    var $ModelClassName = "knjl353cModel";
    var $ProgramID      = "KNJL353C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl353c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl353cModel();          //コントロールマスタの呼び出し
                    $this->callView("knjl353cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl353cCtl = new knjl353cController;
//var_dump($_REQUEST);
?>
