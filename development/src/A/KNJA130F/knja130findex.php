<?php

require_once('for_php7.php');

require_once('knja130fModel.inc');
require_once('knja130fQuery.inc');

class knja130fController extends Controller {
    var $ModelClassName = "knja130fModel";
    var $ProgramID      = "KNJA130F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja130f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja130fModel();        //コントロールマスタの呼び出し
                    $this->callView("knja130fForm1");
                    exit;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knja130f");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja130fCtl = new knja130fController;
?>
