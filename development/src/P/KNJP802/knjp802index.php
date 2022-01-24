<?php

require_once('for_php7.php');

require_once('knjp802Model.inc');
require_once('knjp802Query.inc');

class knjp802Controller extends Controller
{
    public $ModelClassName = "knjp802Model";
    public $ProgramID      = "KNJP802";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "changeGrade":
                case "knjp802":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjp802Model();        //コントロールマスタの呼び出し
                    $this->callView("knjp802Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp802Ctl = new knjp802Controller();
