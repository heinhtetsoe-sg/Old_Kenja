<?php

require_once('for_php7.php');

require_once('knjd152tModel.inc');
require_once('knjd152tQuery.inc');

class knjd152tController extends Controller
{
    public $ModelClassName = "knjd152tModel";
    public $ProgramID      = "KNJD152T";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd152t":
                    $sessionInstance->knjd152tModel();
                    $this->callView("knjd152tForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd152tForm1");
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
$knjd152tCtl = new knjd152tController();
