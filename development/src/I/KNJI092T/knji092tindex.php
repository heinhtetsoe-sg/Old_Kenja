<?php

require_once('for_php7.php');

require_once('knji092tModel.inc');
require_once('knji092tQuery.inc');

class knji092tController extends Controller {
    var $ModelClassName = "knji092tModel";
    var $ProgramID      = "KNJI092T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knji092t":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knji092tModel();      //コントロールマスタの呼び出し
                    $this->callView("knji092tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knji092tCtl = new knji092tController;
var_dump($_REQUEST);
?>
