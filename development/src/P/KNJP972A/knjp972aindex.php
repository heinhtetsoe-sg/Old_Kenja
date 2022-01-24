<?php

require_once('for_php7.php');

require_once('knjp972aModel.inc');
require_once('knjp972aQuery.inc');

class knjp972aController extends Controller
{
    public $ModelClassName = "knjp972aModel";
    public $ProgramID      = "KNJP972A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp972a":                            //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjp972aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjp972aForm1");
                    exit;
                case "csvOutput":
                    if (!$sessionInstance->getCsvOutputModel()) {
                        $this->callView("knjp972aForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp972aCtl = new knjp972aController();
