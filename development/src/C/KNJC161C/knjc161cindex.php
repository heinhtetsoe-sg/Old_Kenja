<?php

require_once('for_php7.php');

require_once('knjc161cModel.inc');
require_once('knjc161cQuery.inc');

class knjc161cController extends Controller
{
    public $ModelClassName = "knjc161cModel";
    public $ProgramID      = "KNJC161C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc161c":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc161cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjc161cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc161cCtl = new knjc161cController();
//var_dump($_REQUEST);
