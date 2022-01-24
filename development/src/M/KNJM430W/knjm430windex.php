<?php

require_once('for_php7.php');

require_once('knjm430wModel.inc');
require_once('knjm430wQuery.inc');

class knjm430wController extends Controller
{
    public $ModelClassName = "knjm430wModel";
    public $ProgramID      = "KNJM430W";     //プログラムID

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "change":              //科目（講座）が変わったとき
                case "change_order":        //出力順が変わったとき
                case "reset":
                case "read":
                case "pre":
                case "next":
                    $sessionInstance->getMainModel();
                    $this->callView("knjm430wForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm430wCtl = new knjm430wController();
