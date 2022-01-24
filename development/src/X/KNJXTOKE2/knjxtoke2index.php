<?php

require_once('for_php7.php');

require_once('knjxtoke2Model.inc');
require_once('knjxtoke2Query.inc');

class knjxtoke2Controller extends Controller {
    var $ModelClassName = "knjxtoke2Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "main":
                    $this->callView("knjxtoke2Form1");
                    break 2;
                case "read":
                case "tree":
                    $this->callView("knjxtoke2Form2");
                    break 2;
                case "toukei3":
                    //分割フレーム作成
                    $args["left_src"]   = "index.php?cmd=tree&SEL_SEMI=" .$sessionInstance->year ."," .$sessionInstance->semester;
                    $args["right_src"]  = "index.php?cmd=main";
                    $args["cols"] = "30%,*";
                    View::frame($args);
                    return;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knjxtoke2index.php?cmd=tree";
                    $args["right_src"]  = "knjxtoke2index.php?cmd=main";
                    $args["cols"] = "30%,*";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjxtoke2Ctl = new knjxtoke2Controller;
//var_dump($_REQUEST);
?>
