<?php

require_once('for_php7.php');

require_once('knja133mModel.inc');
require_once('knja133mQuery.inc');

class knja133mController extends Controller {
    var $ModelClassName = "knja133mModel";
    var $ProgramID      = "KNJA133M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja133m":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja133mModel();        //コントロールマスタの呼び出し
                    $this->callView("knja133mForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("print");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja133mCtl = new knja133mController;
?>
