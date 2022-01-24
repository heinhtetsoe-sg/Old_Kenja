<?php

require_once('for_php7.php');

require_once('knjd187vModel.inc');
require_once('knjd187vQuery.inc');

class knjd187vController extends Controller
{
    public $ModelClassName = "knjd187vModel";
    public $ProgramID      = "KNJD187V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "semester":
                case "knjd187v":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd187vModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd187vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187vCtl = new knjd187vController();
