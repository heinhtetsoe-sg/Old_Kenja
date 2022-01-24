<?php

require_once('for_php7.php');

require_once('knjb104dModel.inc');
require_once('knjb104dQuery.inc');

class knjb104dController extends Controller
{
    public $ModelClassName = "knjb104dModel";
    public $ProgramID      = "KNJB104D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "gakki":
                    $sessionInstance->knjb104dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjb104dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb104dCtl = new knjb104dController();
//var_dump($_REQUEST);
