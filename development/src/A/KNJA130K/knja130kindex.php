<?php

require_once('for_php7.php');

require_once('knja130kModel.inc');
require_once('knja130kQuery.inc');

class knja130kController extends Controller {
    var $ModelClassName = "knja130kModel";
    var $ProgramID      = "KNJA130K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja130k":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja130kModel();        //コントロールマスタの呼び出し
                    $this->callView("knja130kForm1");
                    exit;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knja130k");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja130kCtl = new knja130kController;
?>
