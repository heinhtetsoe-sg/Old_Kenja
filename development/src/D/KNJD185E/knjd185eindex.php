<?php

require_once('for_php7.php');

require_once('knjd185eModel.inc');
require_once('knjd185eQuery.inc');

class knjd185eController extends Controller
{
    var $ModelClassName = "knjd185eModel";
    var $ProgramID      = "KNJD185E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd185e":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd185eModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd185eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd185eCtl = new knjd185eController();
?>
